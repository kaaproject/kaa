from os import listdir
from os.path import isfile, join
import yaml
import json

config_path = "config/"
config_files = [f for f in listdir(config_path) if isfile(join(config_path, f))]

applications = {}

for file in config_files:

    with open("{}{}".format(config_path, file), 'r') as stream:
        try:
            applications[file.replace(".yaml", "")] = {"kaa": { "applications": yaml.safe_load(stream)["kaa"]["applications"]}}
        except yaml.YAMLError as exc:
            print(exc)

print(json.dumps(applications, indent=4, sort_keys=True))
