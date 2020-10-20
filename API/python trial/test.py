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
            current_time = int(round(time.time()*1000))
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

    def getAssetIDByName(self, name):
        _token = self.getFlowgateToken()
        api_url = self.host + "/apiservice/v1/assets/name/"+name+"/"
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


if __name__ == '__main__':
    urllib3.disable_warnings()
    client = FlowgateClient()
    token = client.getFlowgateToken()

    print("Token:")
    print(token)

    print('Info for "testServer":')
    res = client.getAssetIDByName('testServer')
    print(res)
