'use strict';

// Deps
var gulp = require('gulp');
var del = require('del');
var wiredep = require('wiredep').stream;
var runSequence = require('run-sequence');
var mainBowerFiles = require('main-bower-files');
var browserSync = require('browser-sync');
var browserSyncSpa = require('browser-sync-spa');
var $ = require('gulp-load-plugins')();
var nodemon = require('gulp-nodemon');
var gulpNgConfig = require('gulp-ng-config');
var runSequence = require('run-sequence');


var bower = require('gulp-bower');

gulp.task('bower', function() {
  return bower()
    .pipe(gulp.dest(config.dist))
});



var config = {
    dist: 'dist',
    src: 'app',
    tmp: '.tmp',
    errorHandler: function(name) {
        return function(e) {
            $.util.log($.util.colors.red(name + e.toString()));
            this.emit('end');
        };
    },
    wiredep: {
        directory: 'bower_components'
    }
};

/**
 * Inject all scss files into index.scss and vendor.scss
 * runs sass and autoprefixer and move all compiled css files to .tmp/styles
 */

 gulp.task('styles', function() {
// gulp.task('styles', ['developpro'], function() {
    var sassOptions = {
        style: 'expanded'
    };

    // non vendor scss files
    var scssFiles = gulp.src([
        config.src + '/**/*.scss',
        '!' + config.src + '/styles/index.scss',
        '!' + config.src + '/styles/vendor.scss',
        '!' + config.src + '/vendor/**/*.scss'
    ]);

    // vendor scss files
    var vendorScssFiles = gulp.src([
        config.src + '/vendor/**/*.scss'
    ]);

    var injectOpts = {
        transform: function (filePath) {
            return '@import \'' + filePath + '\';';
        },
        starttag: '// injector',
        endtag: '// endinjector',
        addRootSlash: false
    };

    var indexFilter = $.filter('index.scss');
    var vendorFilter = $.filter('vendor.scss');

    return gulp.src([
        config.src + '/styles/index.scss',
        config.src + '/styles/vendor.scss'
    ])
    .pipe(indexFilter)
    .pipe($.inject(scssFiles, injectOpts))
    .pipe(indexFilter.restore())
    .pipe(vendorFilter)
    .pipe($.inject(vendorScssFiles, injectOpts))
    .pipe(vendorFilter.restore())
    .pipe($.sourcemaps.init())
    .pipe($.sass(sassOptions)).on('error', config.errorHandler('Sass'))
    .pipe($.autoprefixer()).on('error', config.errorHandler('Autoprefixer'))
    .pipe($.sourcemaps.write())
    .pipe(gulp.dest(config.tmp + '/styles'))
    .pipe(browserSync.reload({stream: true}));
});


/**
 * Compiles template cache
 */
gulp.task('templateCache', function() {
    return gulp.src([
        config.src + '/**/*.tpl.html',
        config.tmp + '/**/*.tpl.html'
    ])
    .pipe($.minifyHtml({
        empty: true,
        spare: true,
        quotes: true
    }))
    .pipe($.angularTemplatecache('templateCacheHtml.js', {
        module: 'gulp.templateCache'
    }))
    .pipe(gulp.dest(config.tmp + '/partials/'));
});


/**
 * Inject all styles and js files into index.html
 */
gulp.task('inject', ['styles', 'templateCache'], function() {
    var vendorCss = gulp.src([
        config.src + '/vendor/**/*.css'
    ]);

    var vendorCssOpts = {
        starttag: '<!-- inject:vendor -->',
        ignorePath: [config.src, config.tmp],
        addRootSlash: false
    };

    // all compiled css files in .tmp
    var tmpCssFiles = gulp.src([
        config.src + '/**/*.css',
        config.tmp + '/styles/**/*.css',
        '!' + config.tmp + '/styles/vendor.css',
        '!' + config.src + '/vendor/**/*'
    ], {read: false});

    // all non script js files
    var jsFiles = gulp.src([
        config.src + '/**/*.js',
        '!' + config.src + '/scripts/**/*.js'
    ]);

    var injectOpts = {
        // remove src/ or .tmp/ in the injected url string
        ignorePath: [config.src, config.tmp],
        addRootSlash: false
    };

    // all script js files, angular file sort
    var injectScripts = gulp.src([
        config.src + '/scripts/**/*.js',
        '!' + config.src + '/scripts/**/*.spec.js',
        '!' + config.src + '/scripts/**/*.mock.js'
    ])
    .pipe($.angularFilesort()).on('error', config.errorHandler('AngularFilesort'));

    var scriptOpts = {
        // remove src/ or .tmp/ in the injected url string
        starttag: '<!-- inject:scripts -->',
        ignorePath: [config.src, config.tmp],
        addRootSlash: false
    };

    var partialsInjectFile = gulp.src(config.tmp + '/partials/templateCacheHtml.js', {read:false});
    var partialsInjectOptions = {
        starttag: '<!-- inject:partials -->',
        ignorePath: config.tmp,
        addRootSlash: false
    };

    return gulp.src(config.src + '/*.html')
        // partials
        .pipe($.inject(partialsInjectFile, partialsInjectOptions))
        .pipe($.inject(tmpCssFiles, injectOpts))
        .pipe($.inject(vendorCss, vendorCssOpts))
        .pipe($.inject(jsFiles, injectOpts))
        .pipe($.inject(injectScripts, scriptOpts))
        .pipe(wiredep(config.wiredep))
        .pipe(gulp.dest(config.tmp))
        .pipe(browserSync.reload({stream: true}));
});


/**
 * takes .tmp/index.html injected refs
 * compiles into rev files
 */
gulp.task('buildIndex', function() {
    var assets;
    var cssFilter = $.filter('**/*.css');
    var jsFilter = $.filter('**/*.js');
    var htmlFilter = $.filter('*.html');

    return gulp.src(config.tmp + '/*.html')
    // all assets in index.html
    .pipe(assets = $.useref.assets())
    .pipe($.rev())
    //js
    .pipe(jsFilter)
    .pipe($.uglify({ preserveComments: 'some'})).on('error', config.errorHandler('Uglify'))
    .pipe(jsFilter.restore())
    // css
    .pipe(cssFilter)
    .pipe($.csso())
    .pipe(cssFilter.restore())
    // all
    .pipe(assets.restore())
    .pipe($.useref())
    .pipe($.revReplace())
    // html
    .pipe(htmlFilter)
    .pipe($.minifyHtml({
        empty: true,
        spare: true,
        quotes: true,
        conditionals: true
    }))
    .pipe(htmlFilter.restore())
    .pipe(gulp.dest(config.dist + '/'));
});

/**
 * Delete dist and tmp folders
 */
gulp.task('clean', function(done) {
    del([config.dist, config.tmp, 'production'], {force: true}, done);
});

/**
 * Only applies for fonts from bower dependencies
 */
gulp.task('fonts', function () {
    return gulp.src('bower_components/components-font-awesome/fonts/*')
    .pipe(gulp.dest(config.dist + '/fonts/'));
});


gulp.task('copyfonts', function () {
    return gulp.src('bower_components/lato/font/**/*', { base: 'bower_components/lato/font/' })
    .pipe(gulp.dest(config.dist + '/font/'));
});


/**
 * imagemin all app/images, and moves to dist/images
 */
gulp.task('images', function() {
    var imageminOpts = {
        optimizationLevel: 3,
        progressive: true,
        interlaced: true
    };

    return gulp.src([config.src + '/images/**/*', config.src + '/favicon.ico'])
        .pipe($.imagemin(imageminOpts))
        .pipe(gulp.dest(config.dist + '/images'))
        .pipe($.size());
});


gulp.task('assets', function() {
    var otherFiles = gulp.src(
        '!bower_components/*.{css,html,js,scss}'
    );

    return otherFiles
    .pipe(gulp.dest(config.dist));

});


/**
 * Watchers
 */
gulp.task('watch', function() {
    // index or bower changes run inject for wiredep
    gulp.watch([config.src + '/*.html', 'bower.json'], ['inject']);

    // watch .css or .scss files
    gulp.watch([
        config.src + '/styles/**/*.css',
        config.src + '/**/*.scss'
    ], function(event) {
        if (event.type === 'changed') {
            gulp.start('styles');
        } else {
            gulp.start('inject');
        };
    });

    // watch .js files
    gulp.watch(config.src + '/**/*.js', function(event) {
        if (event.type === 'changed') {
            browserSync.reload();
        } else {
            gulp.start('inject');
        };
    });

    // watch html files
    gulp.watch(config.src + '/**/*.html', function(event) {
        gulp.start('inject');
        browserSync.reload(event.path);
    });
});

function startBrowserSync(baseDir) {
    browserSync.init({
        ghostMode: false,
        server: {
            baseDir: baseDir,
            routes: {
                '/bower_components': 'bower_components'
            }
        },
        port: 3000
    });
}

gulp.task('nodemon', function (cb) {  
    var started = false;
    return nodemon({
        script: 'server.js'
        , env: { 'NODE_ENV': 'development' }
    }).on('start', function () {
        if (!started) {
            cb();
        } 
        started = true; 
    })
});


gulp.task('serve', ['clean'], function() {
    runSequence('inject',
                'watch',
                'nodemon', function(){
                    startBrowserSync([config.tmp, config.src]);
                });
});


//publish/production environment
gulp.task('publish', ['clean'], function () {
    runSequence('styles',
                'templateCache',
                'inject',
                'buildIndex',
                'fonts',
                'copyfonts',
                'images', function(){
                    return gulp.src(['tools/keys/*','dist/***/**/*','docker/*','dist/index.html','package.json', 'server.js', 'userConfig.json', 'nodemon.json'], {base: './'})
                    .pipe(gulp.dest('production'));
                });
})


