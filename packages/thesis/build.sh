DIRECTORY=./src

# Create a shadow directory for the build
# mkdir -p $DIRECTORY/_content
# for i in $DIRECTORY/content/*mmd; do
#     xbase=${i##*/}
#     xpref=${xbase%.*}
#     ./node_modules/.bin/mmdc -i $i -o $DIRECTORY/_content/${xpref%.*}.svg
#     inkscape -D $DIRECTORY/_content/${xpref%.*}.svg  -o $DIRECTORY/_content/${xpref%.*}.png
#     rm $DIRECTORY/_content/${xpref%.*}.svg
# done

for i in $DIRECTORY/content/*png; do
    xbase=${i##*/}
    xpref=${xbase%.*}
    cp $i $DIRECTORY/_content/${xpref%.*}.png
done

docker run --rm -t --user="$(id -u):$(id -g)" --net=none -v "$(pwd):/tmp" leplusorg/latex latexmk -outdir=/tmp/dist -pdf -cd -jobname=thesis -halt-on-error /tmp/src/main.tex
