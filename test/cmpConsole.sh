#!/bin/bash
PARAMS=$1

compress() {
    cd console

    uglifyjs base.js -c --output base.min.js
    uglifyjs console.js -c --output console.min.js

    gzip -c base.min.js > base.js.gz
    gzip -c console.css > console.css.gz
    gzip -c console.html > console.html.gz
    gzip -c console.min.js > console.js.gz
    gzip -c login.html > login.html.gz

    cd ..
}

removeGz() {
    cd console

    rm -Rf base.min.js
    rm -Rf console.min.js
    rm -Rf base.js.gz
    rm -Rf console.js.gz
    rm -Rf console.css.gz
    rm -Rf console.html.gz
    rm -Rf login.html.gz

    cd ..
}

if [ "x$PARAMS" = "x" ]; then
    compress
else
    removeGz
fi


