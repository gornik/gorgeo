gorgeo
======

Provides a facet for grouping documents containing geolocation data based on 
the geohash cells. It groups the geographical points by using 
[geohash](http://en.wikipedia.org/wiki/Geohash) cells, according to the 
level provided by user.

### Installation

TODO

### Example usage

Query example:

```json
  "geohashcell": {
    "field": "location",
    "level" : 3,
    "top_left": "11.21,51.33",
    "bottom_right": "-20.21,80.65"
}
```



Result example:

```json
   "facets": {
      "f": {
         "_type": "Z2VvaGFzaGNlbGw=",
         "field": "location",
         "top_left": "[11.21, 51.33]",
         "bottom_right": "[-20.21, 80.65]",
         "level": 4,
         "base_map_level": 1,
         "counts": {
            "[8.876953125, 76.46484375]": "1",
            "[6.240234375, 79.98046875]": "1",
            "[6.943359375, 79.98046875]": "13",
            "[9.755859375, 77.87109375]": "2",
            "[2.197265625, 72.94921875]": "1",
            "[9.931640625, 77.51953125]": "3",
            ...
         }
   }
```

### Parameters

| Parameter name   | Optional   | Default value     | Description                                                                                            
| ---------------- | :--------: | :---------------: | -------------
| __field__        | _optional_ | "location"        | Name of the document field containing geographical location.
| __top_left__     | _optional_ | "90,-180"         | Geographical coordinates (lattitude, longitude) of the top left point of the current map viewport.
| __bottom_right__ | _optional_ | "-90,180"         | Geographical coordinates (lattitude, longitude) of the bottom right point of the current map viewport.
| __level__        | _optional_ | 3                 | The grouping level (see [Algorithm](#Algorithm)).

_Note_: the map viewport defaults to a full map.

### Results

 * __field__ - equal to the __field__ parameter 
 * __top_left__ - equal to the __top_left__ parameter
 * __bottom_right__ - equal to the __bottom_right__ parameter
 * __level__ - actual grouping level (see [Algorithm](#Algorithm))
 * __base_map_level__ - base grouping level for the current map viewport
 * __counts__ - grouping results, contain cell center and corresponding 
   document count

### Algorithm

The facet counts the documents based on geohash prefixes. For more information
on how geohashing works, see the following links:

 * [Wikipedia on Geohash](http://en.wikipedia.org/wiki/Geohash)
 * [Visualizing Geohash](http://www.bigdatamodeling.org/2013/01/intuitive-geohash.html)

The algorithm groups documents based on geohash prefixes of the stored 
geographical data. The __level__ parameter provided to the facet is used as 
a lenght of the geohash prefix for grouping, i.e. increasing the level by one,
divides the map into 32 cells.

Additionally the facet adapts to the current map viewport by comparing the 
current viewport size with sizes of the geohash cells and calculates the
__base_map_level__. This level is than added to the level parameter and used 
as a geohash prefix. The result is always in [1,6] range.

### To do
 * better parameter checking
 * more tests

Credits
=======
Thanks to [Mahesh Paolini-Subramanya](https://github.com/dieswaytoofast) for his help.
