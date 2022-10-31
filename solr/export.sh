#!/bin/bash

curl --silent --user admin:district \
    'http://localhost:8080/api/tracker/trackedEntities?trackedEntityType=nEenWmSyUEp&ouMode=ACCESSIBLE&fields=trackedEntity,trackedEntityType,orgUnit,attributes\[attribute,displayName,valueType,value\]' \
    | jq '.instances | map({trackedEntity,trackedEntityType,orgUnit} * (.attributes | map({(.attribute): .value}) | add))' > test.json
    # | jq '.instances | map({trackedEntity,trackedEntityType,orgUnit,attributes: (.attributes | INDEX(.attribute))})' > test.json
    # | jq --compact-output '.instances | .[]' > test.jsonlines
    # | jq '.instances' > test.json

# | jq '.instances | map({trackedEntity,trackedEntityType,orgUnit} * (.attributes | map({(.attribute): .value}) | add))'
# leads to
# {
#   "trackedEntity": "k68SkK5yDH9",
#   "trackedEntityType": "nEenWmSyUEp",
#   "orgUnit": "DiszpKrYNg8",
#   "w75KJ2mc4zz": "John",
#   "zDhUuAYrxNC": "Doe"
# },

# | jq '.instances | map({trackedEntity,trackedEntityType,orgUnit,attributes: (.attributes | map({(.attribute): .value}) | add)})' > test.json
# leads to
# "attributes": {
#   "w75KJ2mc4zz": "John",
#   "zDhUuAYrxNC": "Doe"
# }
