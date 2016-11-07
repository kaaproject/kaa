#!/bin/sh
#
# Copyright 2014-2016 CyberVision, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -e

curr_tag=$(git tag --contains)
gh_pages=gh-pages
DOCS_ROOT=docs

if [ x"$curr_tag" = x ]; then
  curr_tag="current"
fi

update_subtree () {
  if [ x"$gh_pages" = x"$(git rev-parse --symbolic-full-name --abbrev-ref HEAD)" ]; then
    if [ -d "$1" ]; then
      git subtree merge --prefix="$1" "$2" -m "$3"
      echo "merged $2 in $1"
    else
      git subtree add --prefix="$1" "$2" -m "$3"
      echo "added $2 in $1"
    fi
  fi
}

if [ -d doc ]; then
  echo "Test deploy for $curr_tag"
  jekyll_root=test-gh-pages-$curr_tag
  latest=$curr_tag
  if [ ! -d $jekyll_root ]; then
    echo "Generating directory structure"
    mkdir -p $jekyll_root
    cp -R gh-pages-stub/* $jekyll_root
    mkdir -p $jekyll_root/$DOCS_ROOT
    mkdir -p $jekyll_root/_data
    ln -s "$PWD/doc" "$PWD/$jekyll_root/$DOCS_ROOT/$curr_tag"
  fi
  cd $jekyll_root
  printf "%s\nversion: %s \ndocs_root: %s" "---" "$latest" "$DOCS_ROOT" > _data/config.yml
  echo "Generating menu data"
  ruby scripts/create_global_toc.rb
  jekyll serve "$@"
elif [ x"$gh_pages" = x"$(git rev-parse --symbolic-full-name --abbrev-ref HEAD)" ]; then
  versions=$(git tag)
  jekyll_root=$PWD
  latest=$(git tag | sort -V -r | head -1)
  gh_pages_stub_br=$latest
  echo "Merging gh-pages-stub from $gh_pages_stub_br"
  git checkout $gh_pages_stub_br
  GH_PAGES_STUB=$(git subtree split --prefix=gh-pages-stub/)
  git checkout -
  git merge "$GH_PAGES_STUB" -m "merged jekyll files"
  if [ -f .nojekyll ]; then
     echo "Removing .nojekyll"
     rm .nojekyll
     git commit .nojekyll -m "Removed .nojekyll file"
  fi
  mkdir -p "$jekyll_root/_data"
  for version in $versions; do
    if [ x"" != x"$(echo "$version" | grep -E "^v[0-9]+\.[0-9]+\.[0-9]+$")" ]; then
      echo "$version"
      release=$version
      release_doc="release/doc/$version"
      git checkout "$release"
      if [ -d doc ]; then
        git subtree split --prefix=doc -b "$release_doc"
        git checkout $gh_pages
        update_subtree "$DOCS_ROOT/$version" "$release_doc" "Merged $release branch in the $gh_pages"
      fi
    fi
  done
  git checkout $gh_pages
  rm -rf test-gh-pages-*
  printf "%s\nversion: %s \ndocs_root: %s" "---" "$latest" "$DOCS_ROOT" > _data/config.yml
  echo "Generating menu data"
  ruby scripts/create_global_toc.rb
  git add _data/menu.yml _data/config.yml _data/versions.yml
  git commit -m "Updated global toc and version"
  echo "Finished deploy into gh-pages"
else
  echo "Nothing to do"
fi
