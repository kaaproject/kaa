file(REMOVE_RECURSE
  "libkaac.pdb"
  "libkaac.dylib"
)

# Per-language clean rules from dependency scanning.
foreach(lang C)
  include(CMakeFiles/kaac.dir/cmake_clean_${lang}.cmake OPTIONAL)
endforeach()
