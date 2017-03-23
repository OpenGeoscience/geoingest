import glob
import json
import os
import itertools

import click

def writeJsonFile(jsonFile, jsonString):
    """Writes a json string to a json file"""

    if os.path.exists(jsonFile):
        os.remove(jsonFile)
    with open(jsonFile, 'w') as outfile:
        json.dump(jsonString, outfile)

    return jsonFile

def generateTemplate(layerName):

    fileName = layerName.strip()
    layerName = os.path.splitext(fileName)[0]
    s3Layer = "s3://kitware-weld-etl-test/{}".format(fileName)

    return {
        "format": "geotiff",
        "name": layerName,
        "cache": "NONE",
        "backend": {
            "type": "s3",
            "path": s3Layer
        }
    }

    
def generateInputJson(layerList):
    """Generates the json input file for a given list"""

    jsonString = [generateTemplate(i) for i in layerList]
    writeJsonFile("json/input.json", jsonString)
    
def createInputFile(start, end):
    """Creates the input json spec"""
    start = int(start)
    end = int(end)

    with open('missing.out') as f:
        lines = f.readlines()

    generateInputJson(lines[start:end])

    
    
@click.command()
@click.argument('start', nargs=1)
@click.argument('end', nargs=1)

def main(start, end):
    """ DATA: Input geotiff or folder of geotiffs """

    createInputFile(start, end)
    
