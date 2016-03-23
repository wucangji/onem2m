# dslink-java-onem2m

Integration for onem2m systems.

## Building and running distributions

1. Run `./gradlew build distZip`
2. Navigate into `build/distributions`
3. Extract the distribution tar/zip
4. Navigate into the extracted distribution
5. Run `./bin/dslink-java-onem2m -b http://localhost:2080/conn`

Note: `http://localhost:2080` is the url to the DSA broker that needs to have been installed prior.


## How to create Container

* Set labels can support 2 type:  
	* ["string1", "string2", ...]
	
	![](Fig/containerLabel1.png)
	
	* [string1, string2, ...]
	
	![](Fig/containerLabel2.png)	

