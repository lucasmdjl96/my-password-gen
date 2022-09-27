((config) => {
    const cssRule = config.module.rules.find(r => "test.css".match(r.test));
    if (!cssRule) {
        throw new Error("Could not find a webpack rule match .css files");
    }
    cssRule.use.push({
        loader: "postcss-loader",
        options: {}
    });
})(config);