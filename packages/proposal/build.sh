docker run --rm -t --user="$(id -u):$(id -g)" --net=none -v "$(pwd):/tmp" leplusorg/latex latexmk -outdir=/tmp/dist -pdf -cd -jobname=proposal /tmp/src/main.tex
