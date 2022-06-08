
# Compilation and use in OSX Big Sur (Mac M1)

## Java JDK 17 
https://www.oracle.com/java/technologies/downloads/

Download the JDK 17 for OSX. 

You can also install maven and ant to compile your software:

`brew install maven ant`

## OpenCV 4.5.5 
`brew install opencv`

## OpenBlas

`brew install openblas`

### Install a custom version 

https://blog.sandipb.net/2021/09/02/installing-a-specific-version-of-a-homebrew-formula/

Openblas 0.3.20 is not yet published in javacpp-presets, and homebrew installs it. 

```
brew tap-new $USER/local-openblas
brew extract --version=0.3.19 openblas $USER/local-openblas
brew install $USER/local-openblas/openblas@0.3.19
```

## Javacv 
Download the Javacv release in the release assets.