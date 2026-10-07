module.exports = function (config) {
  config.set({
    frameworks: ["jasmine", "webpack"],
    files: [{ pattern: "tests/**/*.spec.jsx", watched: false }],
    preprocessors: { "tests/**/*.spec.jsx": ["webpack"] },
    webpack: {
      mode: "development",
      devtool: "inline-source-map",
      resolve: { extensions: [".js", ".jsx"] },
      module: {
        rules: [
          {
            test: /\.[jt]sx?$/,
            exclude: /node_modules/,
            use: {
              loader: "babel-loader",
              options: {
                presets: [
                  ["@babel/preset-env", { targets: { chrome: "120" } }],
                  ["@babel/preset-react", { runtime: "automatic" }],
                ],
                plugins: [
                  ["istanbul", { include: ["src/**/*.js", "src/**/*.jsx"] }],
                ],
              },
            },
          },
        ],
      },
    },
    webpackMiddleware: { stats: "errors-only" },
    reporters: ["progress", "coverage"],
    coverageReporter: {
      dir: "coverage",
      reporters: [
        { type: "html", subdir: "html" },
        { type: "text-summary" },
        { type: "json-summary", subdir: "." },
        { type: "lcovonly", subdir: "." },
      ],
    },
    browsers: [process.env.CHROME_BIN ? "ChromeHeadlessCI" : "ChromeHeadless"],
    customLaunchers: {
      ChromeHeadlessCI: {
        base: "ChromeHeadless",
        flags: ["--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"],
      },
    },
    singleRun: true,
    autoWatch: false,
    client: { jasmine: { random: false } },
    browserNoActivityTimeout: 60000,
  });
};
