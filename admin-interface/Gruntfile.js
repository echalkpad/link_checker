module.exports = function(grunt) {
grunt.initConfig({
  browserify: {
    dist: {
      files: {
        'client/build/js/browser.bundle.js': ['client/js/**/*.js']
      }
    }
  },
  jshint: {
      all: ['Gruntfile.js', '*.js', 'server/**/*.js', 'client/js/**/*.js']
  },
  sass: {
    dist: {
      files: {
        'client/build/css/main.css': 'client/scss/main.scss'
      }
    }
  },
  useminPrepare: {
    html: 'client/index.html',
    options: {
      dest: 'client/dist'
    }
  },
  usemin: {
    html: 'client/dist/index.html'
  },
  filerev: {
    dist: {
      src: ['client/dist/css/main.min.css', 'client/dist/js/browser.bundle.min.js']
    }
  },
  uglify: {
    dist: {
      files: {
        'client/dist/js/browser.bundle.min.js': 'client/build/js/browser.bundle.js'
      },
      options: {
        sourceMap: true
      }
    }
  },
  copy: {
    html: {
      src: 'client/index.html',
      dest: 'client/dist/index.html'
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
                  },
                  {
                      match: /\/\*\s*DEVONLY[\s\S]*?ENDDEVONLY\s*\*\//gm,
                      replacement: ""
                  }
              ]
          },
          files: [
              {expand: true, flatten: true, src: ['client/index.html'], dest: 'client/dist/'}
          ]
      }
  },
  cdnify: {
    all: {
      options: {
        rewriter: function(url) {
           if (url.indexOf('data:') === 0 ||
              url.indexOf('//') === 0 ||
              url.indexOf('http') === 0) {
            return url; // leave data and absolute URIs untouched
          }

          return "{{CDN_SERVER}}" + url;
        },
      },

      files: [{
        expand: true,
        flatten: true,
        src: ['client/dist/*.html'],
        dest: 'client/dist/'
      }]
    }
  }
});

grunt.loadNpmTasks('grunt-browserify');
grunt.loadNpmTasks('grunt-cdnify');
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
grunt.registerTask('dist', ['replace', 'default', 'cssmin', 'uglify', 'filerev', 'usemin', 'cdnify:all']);
};
