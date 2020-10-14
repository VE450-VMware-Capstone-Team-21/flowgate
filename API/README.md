## Flowgate API

Get Flowgate API documentation:

```shell
wget -o api.html https://10.11.16.36/apidoc/index.html --no-check-certificate
```



##### ==ignore== Using Curl 

Our server is not certified, so it needs to add `-k` when `curl`. 

`10.11.16.36` is the IP address of the server (may change always, Login https://202.121.180.29/ -> 虚拟机 -> Flowgate5 -> 控制台 to check the IP address). `https://10.11.16.36/ui/nav/setting/asset-list` is the address after clicking `Setting -> Asset Management`.

Get asset "testServer":

```shell
curl -k 'https://10.11.16.36/ui/nav/setting/asset-list/testServer' -i -X GET
```

But the result seems nonsense... this is only the website info...



#### Using Python

*https://github.com/vmware/flowgate/blob/c8a471acd9950864f0d82093710d5bdbd5deac80/contrib/EdgeXfoundry/app/api/flowgateapi.py*

先获取token再调用api

Token: https://ipaddress/apiservice/v1/auth/token

api: https://ipaddress/apiservice/v1/assets/... (按API Docs来)



#### Using Postman

**Get token:**

Build -> Create a Request:

```http
POST https://10.11.16.36/apiservice/v1/auth/token
```

On *Headers*: set `Content-Type` as `application/json`;

On *Body*: set `raw -> JSON` and fill in

```json
{
    "userName": "admin",
    "password": "Ar_InDataCenter_450"
}
```

Then click *Send*.

Received token example (Body, with status 200 OK):

```json
{
    "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlzcyI6IkZsb3dnYXRlIiwiZXhwIjoxNjAyNjcwNzUzLCJpYXQiOjE2MDI2NjM1NTMsInVzZXJJZCI6ImUxZWRmdjg5NTMwMDIzNzk4Mjc4OTZhMWFhaXFvb3NlIn0.7eFVcYJ5J7CYerNzlzM-dqaiO_0IsJPiv5tG-3ehbwo",
    "expires_in": 1602670753275
}
```

**getAssetIDByName:**

Check asset we have: https://10.11.16.36/ui/nav/setting/asset-list

`https://ipaddress/apiservice/v1/assets/...` (See API Docs)

Create a request:

```http
GET https://10.11.16.36/apiservice/v1/assets/name/testServer
```

On *Headers*: set `Content-Type` as `application/json`;

On *Authorization*: set `TYPE` as `Bearer Token`, and enter the received token above in `Token`.

Then click *Send*.

Received info example:

```json
{
    "id": "4e45aa5d59ed4f9b9e3b5d493bf7e3d2",
    "assetNumber": 10000001,
    "assetName": "testServer",
    "assetSource": "flowgate",
    "category": "Server",
    "subCategory": "Standard",
    "manufacturer": "M1",
    "model": "model1",
    "serialnumber": "422987350",
    "tag": "tag1",
    "assetAddress": null,
    "region": "A",
    "country": "China",
    "city": "Shanghai",
    "building": "JI",
    "floor": "4",
    "room": "404",
    "row": "4",
    "col": "4",
    "extraLocation": null,
    "cabinetName": "cabinet1",
    "cabinetUnitPosition": 0,
    "mountingSide": "Front",
    "capacity": 0,
    "freeCapacity": 0,
    "cabinetAssetNumber": null,
    "assetRealtimeDataSpec": {
        "unit": null,
        "validNumMin": null,
        "validNumMax": null
    },
    "metricsformulars": {},
    "lastupdate": 1602581531579,
    "created": 1602577715891,
    "pdus": null,
    "switches": null,
    "status": {
        "status": "Active",
        "pduMapping": null,
        "networkMapping": null
    },
    "parent": null,
    "tenant": null,
    "justificationfields": {}
}
```

