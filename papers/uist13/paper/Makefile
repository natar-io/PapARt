
TEX=rubber -d	
# TEX=pdflatex

# all : paper.pdf CHI-extended-abstract-copyright.pdf CHI-extended-abstract.pdf CHI-extended-abstract-sample.pdf 

lite : paper-lite.pdf
paper : paper.pdf paper.bib

all : paper.pdf paper-lite.pdf paper-good.pdf


paper-lite.pdf : paper.pdf
	gs -q -dSAFER -dNOPAUSE -dBATCH -sDEVICE=pdfwrite -dPDFSETTINGS=/ebook -sOUTPUTFILE=paper-lite.pdf -f paper.pdf

paper-good.pdf : paper.pdf
	gs -q -dSAFER -dNOPAUSE -dBATCH -sDEVICE=pdfwrite -dPDFSETTINGS=/printer -sOUTPUTFILE=paper-good.pdf -f paper.pdf

%.pdf : %.tex
	$(TEX) $<; $(TEX) $<

