# deemix-webui

This is just the WebUI for deemix, it should be used with deemix-pyweb or something like that

## What's left to do?

- [ ] Use Vue app-wide
  - First step: rewrite the app in Single File Components way ✅
  - Second step: Implement routing for the whole app using Vue Router ⚒
  - Third step: Remove jQuery
- [ ] Implement custom contextmenu ⚒
  - Copy and paste functions ✅
  - Copy Link where possible ✅
  - Download Quality ✅
  - Copy Image URL where possible ✅
  - Resolve problem when positioning out of window (e.g. clicking on the bottom of the window)
  - Resolve problem when right clicking on element and then right clicking in another side (menu does not update)
- [ ] Make i18n async (https://kazupon.github.io/vue-i18n/guide/lazy-loading.html)
  - Use ES2020 async imports, if possible
- [ ] Make the UI look coherent
  - Style buttons
  - Style text inputs
  - Style checkboxes
- [ ] Search tab
	- Better placeholer before search
- [ ] Link Analyzer
	- Better placeholer before analyzing and error feedback
- [ ] Settings tab
	- Maybe tabbing the section for easy navigation
	- Could use a carousel, but it's not worth adding a new dep
  - Variable selector near template inputs
- [ ] Block selection where it's not needed (keep only titles artists albums labels and useful data)
  - There's a SASS mixin for this. Need to use it in the proper classes
- [ ] Better feedback for socket.io possible errors
- [ ] Remove images size limit and add warning if > 1200
- ?

# License

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
