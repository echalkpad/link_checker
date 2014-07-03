module.exports = function(grunt) {
grunt.initConfig({
  browserify: {
    dist: {
      files: {
        'build/js/browser.bundle.js': ['js/**/*.js']
      }
    }
  },
  sass: {
    dist: {
      files: {
        'build/css/main.css': 'scss/base.scss'
      }
    }
  },
  useminPrepare: {
    html: 'index.html',
    options: {
      dest: 'dist'
    }
  },
  usemin: {
    html: ['index.html'],
    css: ['build/css/**/*.css'],
    js: ['build/js/**/*.js']
  },
  filerev: {
    dist: {
      src: ['dist/css/main.min.css', 'dist/js/browser.bundle.js']
    }
  },
  uglify: {
    dist: {
      files: {
        'dist/js/browser.bundle.min.js': 'build/js/browser.bundle.js'
      },
      options: {
        sourceMap: true
      }
    }
  }
});

grunt.loadNpmTasks('grunt-browserify');
grunt.loadNpmTasks('grunt-contrib-sass');
grunt.loadNpmTasks('grunt-filerev');
grunt.loadNpmTasks('grunt-replace');
grunt.loadNpmTasks('grunt-usemin');

grunt.registerTask('default', ['sass', 'browserify']);
grunt.registerTask('dist', ['default', 'useminPrepare', 'filerev', 'usemin']);
}
