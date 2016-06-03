#!/bin/bash

versions=`git tag`
curr_tag=`git tag --contains`
curr_branch=$(git rev-parse --symbolic-full-name --abbrev-ref HEAD)
gh_pages=gh-pages
latest='v0.7.0'

if [ x$cur_tag == x ]; then
  curr_tag="current"
fi

function update_subtree {
  if [ $gh_pages == $(git rev-parse --symbolic-full-name --abbrev-ref HEAD) ]; then
    if [ -d "$1" ]; then
      git subtree merge --prefix=$1 $2 -m "$3"
      echo "merged $2 in $1"
    else
      git subtree add --prefix=$1 $2 -m "$3"
      echo "added $2 in $1"
    fi
  fi
}


echo x$gh_pages
if [[ -d doc ]]; then
  echo "test deploy for $curr_tag"
  jekyll_root=test-gh-pages-$curr_tag
  if [ ! -d $jekyll_root ]; then
    git clone .git --branch gh-pages-stub $jekyll_root --single-branch
    mkdir -p $jekyll_root/kaa
    mkdir -p $jekyll_root/_data
    ln -s $PWD/doc $PWD/$jekyll_root/kaa/$curr_tag
    ln -s $PWD/doc $PWD/$jekyll_root/kaa/latest
  fi
  cd $jekyll_root
  ruby scripts/create_global_toc.rb
  jekyll serve
elif [[ $gh_pages == $(git rev-parse --symbolic-full-name --abbrev-ref HEAD) ]]; then
  jekyll_root=$PWD
  git merge gh-pages-stub -m "merged jekyll files"
  mkdir -p $jekyll_root/_data
  for version in $versions; do
    if [[ "$version" =~ ^v[0-9]\.[0-9]\.[0-9]$ ]]; then
      echo $version
      release=$version
      release_doc="release/doc/$version"
      git checkout $release
      git subtree split --prefix=doc -b $release_doc
      git checkout $gh_pages
      update_subtree "kaa/$version" $release_doc "Merged $release branch in the $gh_pages"
    #   git subtree merge --prefix="kaa/$version" $release_doc -m "Merged $release branch in the $gh_pahes"
      if [ x$version == x$latest ]; then
        update_subtree "kaa/latest" $release_doc "Merged $release branch in the $gh_pages"
      fi
      #git branch -D $release_doc
    fi
  done
  rm -rf test-gh-pages-*
  ruby scripts/create_global_toc.rb
  git commit _data/menu.yml -m "Updated global toc"
#  jekyll serve
  echo "Deploy into gh-pages"
else
  echo "Nothing to do"
fi
