import requests
import json
import time
from config import Config
from flask import Response
import certifi
import urllib3


class FlowgateClient():
    current_token = {}

    def __init__(self):
        self.host = Config.FLOWGATE_HOST
        self.username = Config.FLOWGATE_USER
        self.password = Config.FLOWGATE_PASSWORD

    def getFlowgateToken(self):
        if bool(self.current_token):
            current_time = int(round(time.time() * 1000))
            if self.current_token['expires_in'] - current_time > 600000:
                return self.current_token

        token_url = self.host + '/apiservice/v1/auth/token'
        data = {"userName": self.username, "password": self.password}
        headers = {'Content-type': 'application/json'}

        response = requests.post(token_url, data=json.dumps(data), headers=headers, verify=False)
        if response.status_code == 200:
            self.current_token = response.json()
            return self.current_token
        return None

    def getAssetByName(self, name):
        _token = self.getFlowgateToken()
        api_url = self.host + "/apiservice/v1/assets/name/" + name + "/"
        headers = {'Content-type': 'application/json', "Authorization": "Bearer " + _token['access_token']}
        response = requests.get(api_url, headers=headers, verify=False)
        if response.status_code == 200:
            if response.text:
                try:
                    data = response.json()
                except ValueError:
                    print('No json return')
                    return None
                return data
        return None

    def getAssetByID(self, ID: str):
        _token = self.getFlowgateToken()
        api_url = self.host + "/apiservice/v1/assets/" + ID + "/"
        headers = {'Content-type': 'application/json', "Authorization": "Bearer " + _token['access_token']}
        response = requests.get(api_url, headers=headers, verify=False)
        return response.json()

    def getAssetIDByName(self, name):
        data = self.getAssetByName(name)
        return data['id']

    # testing
    def createAssetRealTimeData(self, deviceName):
        _token = self.getFlowgateToken()
        data = self.getAssetByName(deviceName)
        device_id = data['id']
        if device_id is None:
            response = Response()
            response.status_code = 500
            response.set_data("Failed to query device: " + deviceName)
            return response
        device_data = {'id': str(device_id) + "_1603217266200",
                       'assetID': str(device_id), 'values': []}
        value_unit = {'extraidentifier': None, 'key': 'Temperature', 'value': '33',
                      'valueNum': 0.0, 'unit': 'Celsius', 'time': 1603217266200}
        device_data['values'].append(value_unit)
        # print(device_data)

        api_url = self.host + "/apiservice/v1/assets/sensordata/batchoperation"
        headers = {'Content-type': 'application/json', "Authorization": "Bearer " + _token['access_token']}
        response = requests.post(api_url, data=json.dumps([device_data]), headers=headers, verify=False)

        return response

    def insertAssetRealTimeData(self, deviceName):
        _token = self.getFlowgateToken()
        data = self.getAssetByName(deviceName)
        device_id = data['id']

        device_data = {'assetID': device_id, 'time': 1603217266201, 'values': [],
                       'id': str(device_id) + "_1603217266201"}
        value_unit = {'extraidentifier': None, 'key': 'Temperature', 'value': '22',
                      'valueNum': 0.0, 'unit': 'Celsius', 'time': 1603217266201}
        device_data['values'].append(value_unit)

        api_url = self.host + "/apiservice/v1/assets/" + device_id + "/sensordata"
        headers = {'Content-type': 'application/json', "Authorization": "Bearer " + _token['access_token']}

        print(device_data)

        response = requests.post(api_url, data=json.dumps(device_data), headers=headers, verify=False)
        return response

    def getSensorSettings(self, deviceName):
        _token = self.getFlowgateToken()
        data = self.getAssetByName(deviceName)
        device_id = data['id']

        api_url = self.host + "/apiservice/v1/sensors/setting/" + str(device_id) + "/"
        headers = {"Authorization": "Bearer " + _token['access_token']}
        device_data = {"id": str(device_id)}
        response = requests.get(api_url, data=json.dumps(device_data), headers=headers, verify=False)
        return response.json()

    def getSensorRealTimeData(self, deviceName):
        # getServerMetricsData
        _token = self.getFlowgateToken()
        data = self.getAssetByName(deviceName)
        device_id = data['id']

        api_url = self.host + "/apiservice/v1/assets/server/" + str(device_id) + "/" + \
                  "realtimedata?starttime=1603217266200&duration=300000"
        headers = {'Content-type': 'application/json', "Authorization": "Bearer " + _token['access_token']}
        response = requests.get(api_url, headers=headers, verify=False)
        return response.json()

    def sensorMapping(self, serverName, sensorName):
        _token = self.getFlowgateToken()
        server_data = self.getAssetByName(serverName)
        sensor_data = self.getAssetByName(sensorName)
        server_id = server_data['id']
        sensor_id = sensor_data['id']

        new_metricsformulars = {'SENSOR': {'FrontTemperature': {str(sensor_id): str(sensor_id)}}}
        server_data['metricsformulars'] = new_metricsformulars
        print(server_data)

        api_url = self.host + "/apiservice/v1/assets/mappingfacility"
        headers = {'Content-type': 'application/json', "Authorization": "Bearer " + _token['access_token']}
        response = requests.put(api_url, data=json.dumps(server_data), headers=headers, verify=False)
        return response


if __name__ == '__main__':
    urllib3.disable_warnings()
    client = FlowgateClient()
    token = client.getFlowgateToken()

    print("### Token:")
    print(token)

    print('### Info for "testSensor":')
    res = client.getAssetByName('testSensor')
    print(res)

    print('### Insert data:')
    res = client.createAssetRealTimeData('testSensor')
    print(res)

    res = client.getAssetByName('testSensor')
    print(res)

    '''
    print("### device setting:")
    res = client.getSensorSettings('testSensor')
    print(res)
    '''

    print('### Get real time data:')
    res = client.getSensorRealTimeData('testSensor')
    print(res)
