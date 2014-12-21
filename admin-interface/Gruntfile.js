module.exports = function(grunt) {
grunt.initConfig({
  browserify: {
    dist: {
      files: {
        'build/js/browser.bundle.js': ['js/**/*.js']
      }
    }
  },
  jshint: {
      all: ['Gruntfile.js', 'js/**/*.js']
  },
  sass: {
    dist: {
      files: {
        'build/css/main.css': 'scss/main.scss'
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
    html: 'dist/index.html'
  },
  filerev: {
    dist: {
      src: ['dist/css/main.min.css', 'dist/js/browser.bundle.min.js']
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
  },
  copy: {
    html: {
      src: 'index.html',
      dest: 'dist/index.html'
    }
  },
  watch: {
      scripts: {
          files: ['js/**/*.js'],
          tasks: ['browserify'],
          options: {
            livereload: true
          }
      },
      styles: {
          files: ['**/*.scss'],
          tasks: ['sass'],
          options: {
              livereload: true
          }
      }
  },
  replace: {
      dist: {
          options: {
              patterns: [
                  {
                      match: /<!--\s*DEVONLY\s*-->[\s\S]*?<!--\s*ENDDEVONLY\s*-->/gm,
                      replacement: ""
                  }
              ]
          },
          files: [
              {expand: true, flatten: true, src: ['index.html'], dest: 'dist/'}
          ]
      }
  }
});

grunt.loadNpmTasks('grunt-browserify');
grunt.loadNpmTasks('grunt-contrib-concat');
grunt.loadNpmTasks('grunt-contrib-cssmin');
grunt.loadNpmTasks('grunt-jsxhint');
grunt.loadNpmTasks('grunt-contrib-sass');
grunt.loadNpmTasks('grunt-contrib-uglify');
grunt.loadNpmTasks('grunt-contrib-watch');
grunt.loadNpmTasks('grunt-filerev');
grunt.loadNpmTasks('grunt-replace');
grunt.loadNpmTasks('grunt-usemin');

grunt.registerTask('default', ['jshint', 'sass', 'browserify', 'useminPrepare', 'concat']);
grunt.registerTask('dev', ['default', 'watch']);
grunt.registerTask('dist', ['replace', 'default', 'cssmin', 'uglify', 'filerev', 'usemin']);
};
