# Instal
``` py 
> pip install grpc
> pip install grpcio-tools
```
```sh
> slice2py --output-dir ./ice_out ../slice_definition/Bank.ice
> python -m grpc_tools.protoc -I../grpc_protobuff --python_out=./grpc_out --grpc_python_out=./grpc_out ../grpc_protobuff/currency_tracker.proto
```