from FlowgateClient import FlowgateClient
import urllib3

urllib3.disable_warnings()
client = FlowgateClient()
token = client.getFlowgateToken()

#print("### Token:")
#print(token)

print('### Info for "testServer":')
data = client.getAssetByName('testServer')
print(data)

print('### Info for "testSensor":')
data = client.getAssetByName('testSensor')
print(data)

print('### Insert data:')
res = client.insertAssetRealTimeData('testSensor')
print(res)


print('### Read real-time data:')
res = client.getSensorRealTimeData('testServer')
print(res)