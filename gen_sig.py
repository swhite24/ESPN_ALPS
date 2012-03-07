# We like these libraries, feel free to use ones that you like.
from requests import post
from time import time
from hashlib import md5
import urllib
import json
 
# The access key you received when you registered your application.
accessKey = "f786504c72057d52360cd9c776b46693adbd3a90"
 
# The shared secret you received when you registered your application.
# N.B.  This should be kept secret, and shouldn't be human readable in your source code. We use it as a regular here for
#       the sake of clarity. Remember, this value is functions similarly to a password.
sharedSecret = "0HrepxVO4TqNYkZb4CeK7XntWtlUJPXwiMIW70gj"
 
# HTTP headers that are going to be used for every request. The API only accepts things of MIME type "application/json".
httpHeaders = {"Content-Type": "application/json"}
 
# This method generates the signature that is transmitted to authenticate your access key.
def generateSignature(accessKey, sharedSecret):
    # Get the current UNIX time.
    currentTime = int(time())
    # Concatenate:
    #   1. access key
    #   2. shared secret
    #   3. current unix time
    unHashedSignature = "{0}{1}{2}".format(accessKey, sharedSecret, currentTime)
    mdFiveHasher = md5();
    mdFiveHasher.update(unHashedSignature)
    # The signature that you'll actually send.
    signature = mdFiveHasher.hexdigest()
 
# This method logs into the API and returns a token that's valid for +/- five minutes from the time its generated.
def login(userName, password):
    # Create the payload for the login method.
    loginRequestDict = {"username": userName, "password": password}
    # Convert the dict to a JSON string.
    loginRequestPayload = json.dumps(loginRequestDict)
 
    # The base URL used to log into the API. (w/o query parameters)
    loginBaseUrl = "https://api.espnalps.com/prod/login"
 
    queryParams = {
        # The access key that was generated when you registered your app
        "key": accessKey,
        # The signature generated from your shared secret, the current time, and access key
        "signature": generateSignature(accessKey, sharedSecret)
    }
 
    # The URL used to log into the API. (w/ query parameters)
    loginUrl = loginBaseUrl + "?" + urllib.urlencode(queryParams)
 
    # Actually make the API call to login
    response = post(loginUrl, headers = httpHeaders, data = loginRequestPayload)
 
    # Convert the JSON document to a python dictionary.
    responseDict = json.loads(response.content)
 
    # Return the token!
    responseDict["response"]["token"]
