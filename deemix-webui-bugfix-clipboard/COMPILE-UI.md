**NOTE: THIS FILE IS NEEDED JUST FOR DEVELOPERS OF THIS PROJECT, IF YOU AREN'T YOU CAN IGNORE IT**

This file explains how to compile files for the WebUI.

# What you need to do just the first time

1. Download and install Node.js, you can download it [here](https://nodejs.org/en/download/) (also installs npm)

2. Once you have finished to install Node.js, check if everything is ok by running in a terminal the commands

```bash
$ node -v
```
and then
```bash
$ npm -v
```

If you see the corresponding versions of node and npm, you are ready to code!

3. Go to the root of this project, open your favorite terminal and run

```bash
$ npm i
```

# Scripts

## Development

By simply running

```bash
$ npm run dev
```

you will have 3 tasks running at the same time:
- the [Python](https://www.python.org/) server
- the [rollup](https://rollupjs.org/guide/en/) watcher pointing to the configured `.js` file and ready to re-bundle
- the [SASS](https://sass-lang.com/) compiler watching for `.scss` files changes

Note that in development mode 2 more files, `bundle.js.map` and `style.css.map`, will be created in the public folder. These files will be deleted when running the build command, so you don't need to worry about them.

**You can now go to http://127.0.0.1:6595 and see the app running.**

### Editing files

You can edit `.scss` and `.js` files and simply refresh the page to see your new and surely awesome code directly in the app 😉

However, if you need to edit the `public/index.html` file you'll have to kill the terminal and re-run `npm run dev` to see your edits.

### Adding files

If you want to add new super-awesome `.js` files, just add them. Deemix uses ES6 synthax, so you'll probably need to export some functions or variables from your new file. Files that will export and import nothing will be ignored by the bundler (rollup).

If you want to add new mega-awesome `.scss` (style) files, you need to import them in the main `style.scss` file. The `.scss` files **must** all start with an underscore _, except for the `style.scss` file.

## Building

When you want to deploy your application, you **must** run

```bash
$ npm run build
```

This is necessary to get

- a bundled `.js` file **minified**
- the compiled `.css` compressed
- deleted the 2 `.map` files

in order to drop the final application size (we are talking about MBs, the maps are heavy).

# Other

If you notice that another team member installed or updated one or more new packages, just run

```bash
$ npm i
```

and you will be fine.
