/*
 * http://www.mwsoft.jp/programming/java/java_tips_file_read_recursive.html を参考にしました。
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Mizutani
 */
public class SemanticNetLoadFiles {

	SemanticNetLoadFiles(){}

	public SemanticNet SNFilesLoader(String directryName){
		File dir = new File(directryName);
		ArrayList<String> strList = readDirectry(dir);
		SemanticNet sn = addStrListToSemNet(strList);
		return sn;
	}

	/*
	 * 引数の ArrayList<String> strList を区切り文字" "を使って、セマンティックネットにaddしまくる。
	*/
	SemanticNet addStrListToSemNet(ArrayList<String> strList){
		SemanticNet sn = new SemanticNet();
		for(int i=0; i< strList.size(); i++){
			String str = strList.get(i);
			String[] youso = str.split(" ", 0 );
			sn.addLink(new Link(youso[0],youso[1],youso[2], sn));
		}
		return sn;
	}

	/*
	 * ディレクトリ以下の全てのテキストファイルをStringのリストに直してアレする
	*/
	public ArrayList<String> readDirectry( File dir ) {
		ArrayList<String> strLists = new ArrayList<String>();

		File[] files = dir.listFiles();
		if( files == null )
			return strLists;
		for( File file : files ) {
			if( !file.exists() )
				continue;
			else if( file.isDirectory() )
				strLists.addAll(readDirectry( file ));
			else if( file.isFile() )
				strLists.addAll(fileReader( file ));
		}
		return strLists;
	}

	/*
	 *  ファイル一つに対して行う処理。fileが存在し、ファイルであることが前提。
	*/
	public ArrayList<String> fileReader( File file ) {
		ArrayList<String> dataList_str = new ArrayList<String>();
		try{
			if(file.canRead()){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				while((str = br.readLine()) != null){
					if(str.length()>0)
						dataList_str.add(str);
				}
				br.close();
			}else{
				System.out.println("ファイルが見つからないか開けません");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		return dataList_str;
	}

}
