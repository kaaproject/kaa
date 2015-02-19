file(REMOVE_RECURSE
  "libkaac_s.pdb"
  "libkaac_s.a"
)

# Per-language clean rules from dependency scanning.
foreach(lang C)
  include(CMakeFiles/kaac_s.dir/cmake_clean_${lang}.cmake OPTIONAL)
endforeach()
