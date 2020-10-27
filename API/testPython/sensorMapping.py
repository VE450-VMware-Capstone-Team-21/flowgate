# This script is for mapping 'testServer' and 'testSensor' together

from FlowgateClient import FlowgateClient
import urllib3

urllib3.disable_warnings()
client = FlowgateClient()
token = client.getFlowgateToken()


res = client.sensorMapping('testServer', 'testSensor')
print(res)

print('### Info for "testServer":')
data = client.getAssetByName('testServer')
print(data)

print('### Info for "testSensor":')
data = client.getAssetByName('testSensor')
print(data)

