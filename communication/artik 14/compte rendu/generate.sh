#!/bin/bash

## Install npm then :
## sudo npm install -g markdown-styles


generate-md --layout github --input ./compte-rendu.md --output ./output
cp presentation.pdf output/presentation.pdf
cp todo-papart.html output/todo-papart.html
cp papart.xml output/papart.xml
