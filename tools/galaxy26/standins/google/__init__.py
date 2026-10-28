"""bc26 galaxy replica: stand-ins for the Google Cloud client libraries that siarnaq imports.

This regular package shadows the whole `google` namespace on siarnaq's PYTHONPATH, so no real Google client library
can be imported even if one were installed. Each stand-in implements only the calls siarnaq makes (grep of galaxy
f343088, listed in tools/galaxy26/README.md) on top of tools/galaxy26/replica_gcp (local disk). Anything else raises:
a missing name is an AttributeError or ImportError, a known class's unimplemented method raises NotImplementedError.
Nothing here opens a network connection.
"""
STANDIN = 'bc26-replica'
