package ninja;

import java.io.InputStream;

public class ResourceFetch {
	public InputStream fetch_(String path){
		return getClass().getResourceAsStream(path);
	}
	public static InputStream fetch(String path){
		return new ResourceFetch().fetch_(path);
	}
}
