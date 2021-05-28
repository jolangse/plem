#!/usr/bin/python3
import re
import struct
import binascii
import socket
import argparse

verbose = False
PLEM_HOST = "127.0.0.1"
PLEM_PORT = 13579

if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_argument("-n", "--nodeid", help="PLEM Node ID")
    parser.add_argument("-d", "--data", help="Data value")
    parser.add_argument("-s", "--host", help="PLEM Host/server")
    parser.add_argument("-p", "--port", help="PLEM Port")
    parser.add_argument("-v", "--verbose", action="store_true", help="increase output verbosity")
    params =  args = parser.parse_args()
    verbose = params.verbose


    if params.host:
        PLEM_HOST = params.host
    if params.port:
        PLEM_PORT = int(params.port)

    if not re.match(
            '^[0-9a-fA-F]{10}$',
            params.nodeid):
        print("Node ID not 10 hex digits (5 byte ID)")
        exit(1)

    sensor_value = float(params.data)
    if sensor_value < 0 or sensor_value > 262144:
        print("Illumination values are in range 0 to 262144 (16bit)")
        exit(1)

    if verbose: print("Got value:", sensor_value)

    # Protocol Data Unit spec (PDU):
    #  5 bytes preamble: LEMP1
    #  1 NULL-byte terminates preamble
    #  1 byte 0x01 (type: ID)
    #  1 byte 0x05 (length: 5 bytes)
    #  5 bytes node ID
    #  1 byte 0x20 (datatype uint16 illumination value)
    #  1 byte 0x02 (length 2 bytes)
    #  2 bytes data (unsigned int 16 bit illumination in lux)
    tmp = int(round(sensor_value*4096))
    lsb =  (tmp & 0xFF)
    csb =  (tmp & 0xFF00)>>8
    msb =  (tmp & 0xFF0000)>>16

    print ("MSB: ", msb, " CSB: ", csb, " LSB: ", lsb)

    values = (
            b'LEMP1',    # Preamble
            1,          # Type: ID
            5,          # Lexpnght: 5 bytes
            int(params.nodeid[0:2], 16), # Manual build of
            int(params.nodeid[2:4], 16), # 5 bytes ID
            int(params.nodeid[4:6], 16),
            int(params.nodeid[6:8], 16),
            int(params.nodeid[8:10],16),
            16,          # Type: Data type, see SensorType.java
            3,          # Length: 2 bytes
            msb,
            csb,
            lsb
            )

    # Network-endian: s = struct.Struct('!5sxbb5Bbbh')
    s = struct.Struct('!5sxbb5BbbBBB')
    packed_data = s.pack(*values)

    if verbose: print('PLEM Host      :', PLEM_HOST)
    if verbose: print('PLEM Port      :', PLEM_PORT)
    if verbose: print('Original values:', values)
    if verbose: print('Format string  :', s.format)
    if verbose: print('Uses           :', s.size, 'bytes')
    if verbose: print('Packed Value   :', binascii.hexlify(packed_data))

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM) # UDP socket.
    sock.sendto(packed_data, (PLEM_HOST, PLEM_PORT))
