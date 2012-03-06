from time import time
from hashlib import md5

accessKey="96a55a98a49c7f4f795eee184d20414eb8a95178"
sharedSecret="oR72b4paDj3AJmZGMEqswvQMk61FYbsOZSHEICn0"

currentTime = int(time())

stringToHash = "{0}{1}{2}".format(accessKey, sharedSecret, currentTime)

m = md5()
m.update(stringToHash)

signature = m.hexdigest()

print signature
