#/bin/bash 

gs -q -dSAFER -dNOPAUSE -dBATCH -sDEVICE=pdfwrite -dPDFSETTINGS=/ebook -sOUTPUTFILE=poster-lite.pdf -f poster.pdf

gs -q -dSAFER -dNOPAUSE -dBATCH -sDEVICE=pdfwrite -dPDFSETTINGS=/ebook -sOUTPUTFILE=demo-lite.pdf -f demo.pdf

gs -q -dSAFER -dNOPAUSE -dBATCH -sDEVICE=pdfwrite -dPDFSETTINGS=/ebook -sOUTPUTFILE=doct-lite.pdf -f doct.pdf
