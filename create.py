#! /usr/bin/python
import os


sketchbookFolder = "../sketchbook/libraries/"

files = os.listdir(os.getcwd())
curDir = os.getcwd()

for file in files:
  if os.path.isdir(file):
      os.chdir(sketchbookFolder)
      if not os.path.isdir(file):
        print("creating folder " + file + "...")
        os.mkdir(file)
        os.chdir(file)
        os.mkdir("library")
      os.chdir(curDir)

