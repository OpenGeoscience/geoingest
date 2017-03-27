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

def inputTemplate(s3, layerName):

    return [{
        "format": "geotiff",
        "name": layerName,
        "cache": "NONE",
        "numPartitions": 20000,
        "backend": {
            "type": "s3",
            "path": s3
        }
    }]
    
def backendTemplate():
    return {
        "backend-profiles": []
    }

def outputTemplate(catalog):
    return {
        "backend": {
            "type": "s3",
            "path": catalog
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

    
@click.command()
@click.argument('s3', nargs=1)
@click.argument('layername', nargs=1)
@click.argument('catalog', nargs=1)
def main(s3, layername, catalog):
    """ DATA: Input geotiff or folder of geotiffs """

    writeJsonFile("input.json", inputTemplate(s3, layername))
    writeJsonFile("output.json", outputTemplate(catalog))
    writeJsonFile("backend-profiles.json", backendTemplate())

    
