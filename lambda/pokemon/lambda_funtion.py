import lambda_function
event = {
    "queryStringParameters": {
        "param1": "value1"
    },
    "path": "/api",
    "requestContext": {
         "param2": "value2"
    }
}
res = lambda_function.lambda_handler(event=event, context={})
assert 200 == int(res["statusCode"])