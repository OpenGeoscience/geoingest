import glob
import json
import os

import click


def writeJsonFile(jsonFile, jsonString):
    """Writes a json string to a json file"""
    if os.path.exists(jsonFile):
        os.remove(jsonFile)
    with open(jsonFile, 'w') as outfile:
        json.dump(jsonString, outfile)
    return jsonFile

def createInputFile(data, dataFormat):
    """Creates the input json spec"""
    
    if "temporal" in dataFormat.lower():
        ingestFormat = "temporal-geotiff"
    else:
        ingestFormat = "geotiff"

    if os.path.isdir(data):
        name = data
    elif os.path.isfile(data):
        name = os.path.splitext(os.path.basename(data))[0]
    jsonString = [{
        "format": ingestFormat,
        "name": name,
        "cache": "NONE",
        "backend": {
            "type": "hadoop",
            "path": "file://{}".format(data)
        }
    }]
    jsonFile = writeJsonFile('input.json', jsonString)

def createOutputFile(dataFormat):

    jsonString = {
        "backend": {
            "type": "file",
            "path": "catalog"
        },
        "reprojectMethod": "buffered",
        "pyramid": True,
        "tileSize": 256,
        "keyIndexMethod": {
            "type": "zorder"
        },
        "resampleMethod": "cubic-spline",
        "layoutScheme": "zoomed",
        "crs": "EPSG:3857"
    }
    if "temporal" in dataFormat.lower():
        jsonString['keyIndexMethod']['temporalResolution'] = 86400000
        
    jsonFile = writeJsonFile('output.json', jsonString)

    return jsonFile

def createBackendProfiles():
    jsonFile = writeJsonFile('backend-profiles.json',
                             {'backend-profiles': []})
    return jsonFile

def submitIngest():
    os.system("spark-submit --class geotrellis.spark.etl.TemporalMultibandIngest --master 'local[*]' --driver-memory 10G ../jars/geotrellis-spark-etl-assembly-1.0.0-SNAPSHOT.jar --input input.json --output output.json --backend-profiles backend-profiles.json")

def createJsonFiles():
    
    backendProfiles = createBackendProfiles()
    outputJson = createOutputFile(dataFormat)
    inputJson = createInputFile(data, dataFormat)

@click.command()
@click.argument('data', nargs=1,
                type=click.Path(exists=True, resolve_path=True),
                help="Folder of geotiffs or a single geotiff")
@click.argument('dataFormat', nargs=1,
                help="One of these: SinglebandIngest, TemporalSinglebandIngest, MultibandIngest, TemporalMultibandingest")

def main(data, dataFormat):
    """Main function to write necessary json specs"""

    createJsonFiles(data, dataFormat)
    
