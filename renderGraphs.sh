DIRECTORY=./output/graphs

for i in $DIRECTORY/*.mmd; do
    xbase=${i##*/}
    xpref=${xbase%.*}
    npx -p @mermaid-js/mermaid-cli mmdc -i $i -o $DIRECTORY/${xpref%.*}.png
    rm $i;
done

convert -background none -delay 10 -loop 0 +repage $(find $DIRECTORY -iname "attackGraph-node-a-[0-9].png") -set dispose background $DIRECTORY/attackGraph-node-a.gif
convert -background none -delay 10 -loop 0 +repage $DIRECTORY/attackGraph-node-b-%d.png[1-9] $DIRECTORY/attackGraph-node-b.gif
convert -background none -delay 10 -loop 0 +repage $DIRECTORY/attackGraph-node-c-%d.png[1-9] $DIRECTORY/attackGraph-node-c.gif
convert -background none -delay 10 -loop 0 +repage $DIRECTORY/attackGraph-node-d-%d.png[1-9] $DIRECTORY/attackGraph-node-d.gif