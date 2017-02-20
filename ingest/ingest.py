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
        name = os.path.basename(data)
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
    jsonFile = writeJsonFile('json/input.json', jsonString)

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
        
    jsonFile = writeJsonFile('json/output.json', jsonString)

    return jsonFile

def createBackendProfiles():
    jsonFile = writeJsonFile('json/backend-profiles.json',
                             {'backend-profiles': []})
    return jsonFile

def createJsonFiles(data, dataFormat):
    if not os.path.exists('json'):
        os.mkdir('json')
    backendProfiles = createBackendProfiles()
    outputJson = createOutputFile(dataFormat)
    inputJson = createInputFile(data, dataFormat)

@click.command()
@click.argument('data', nargs=1,
                type=click.Path(exists=True, resolve_path=True))
@click.argument('data_format', nargs=1)

def main(data, data_format):
    """ DATA: Input geotiff or folder of geotiffs \n
    DATA_FORMAT: One of these: SinglebandIngest, TemporalSinglebandIngest, MultibandIngest, TemporalMultibandingest"""

    createJsonFiles(data, data_format)
    
