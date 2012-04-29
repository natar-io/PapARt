#! /usr/bin/python
import os


sketchbookFolder = "../sketchbook/libraries/"

files = os.listdir(os.getcwd())

for file in files:
  if os.path.isdir(file):
      print("entering folder " + file + "...")
      os.chdir(file)
      os.system("ant")
      os.system("ln -s dist/" + file +".jar " + sketchbookFolder + file + "/library/")
      os.chdir(os.pardir)

