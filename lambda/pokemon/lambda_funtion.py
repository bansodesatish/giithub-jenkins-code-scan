import json
import requests

def lambda_handler(event, context):
    # Make sure a Pokemon name (or ID) was passed in the event object:
    pokemon = event.get('pokemon')
    if pokemon is None:
        return {
            'statusCode': 400,
            'body': json.dumps('Missing pokemon attribute in the event object')
        }

    # If we have a pokemon name/ID passed in, try to get info for it:
    r = requests.get(f"https://pokeapi.co/api/v2/pokemon/{pokemon}")

    if r.status_code == 200:
        status_code = 200
        body = json.dumps(r.json())
    else:
        status_code = r.status_code
        body = json.dumps(f"Could not load information for Pokemon {pokemon}")

    # Return what we got from the API:
    return {
        'statusCode': status_code,
        'body': body
    }
    