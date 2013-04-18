package forCrawler;

//Ching Yu
//July 14, 2012

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;

public class AndroidCrawler extends JFrame{

	private JButton searchB, stopB, saveB, loadB;
	private JLabel urlL, resultsL, crawlingL;
	private JTextArea resultsTA;
	private JScrollPane resultsSP;
	private Container myCP;
	private boolean crawling;
	private String pageContents;
	private ArrayList<String> appList = new ArrayList(); 
	private int numberOfApps = 0;
	
	public AndroidCrawler(){
		super("Android Crawler");
		setSize(800,600);
		setLocation(280,100);
		myCP = getContentPane();
		myCP.setLayout(null);
		myCP.setBackground(new Color(100, 250, 50));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}//actionPerformed method
		});//WindowListener
		
		myCP = getContentPane();
		myCP.setLayout(null);
		
		urlL = new JLabel("URL: http://f-droid.org/repository/browse/");
		urlL.setSize(400,20);
		urlL.setLocation(25,25);
		urlL.setEnabled(true);
		myCP.add(urlL);
		
		searchB = new JButton("Search");
		searchB.setSize(100,25);
		searchB.setLocation(25,50);
		searchB.setBackground(new Color(100, 200, 40));
		searchB.setEnabled(true);
		myCP.add(searchB);
		searchB.addActionListener(new SearchBHandler());
		
		stopB = new JButton("Stop");
		stopB.setSize(100, 25);
		stopB.setLocation(150,50);
		stopB.setBackground(new Color(100, 200, 40));
		stopB.setEnabled(true);
		myCP.add(stopB);
		stopB.addActionListener(new StopBHandler());
		
		crawlingL = new JLabel("Crawling: OFF");
		crawlingL.setSize(150,20);
		crawlingL.setLocation(25,100);
		crawlingL.setEnabled(true);
		myCP.add(crawlingL);
		
		
		resultsL = new JLabel("Results:");
		resultsL.setSize(150,20);
		resultsL.setLocation(25,130);
		resultsL.setEnabled(true);
		myCP.add(resultsL);
		
		resultsTA = new JTextArea("");
		resultsSP = new JScrollPane(resultsTA,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultsSP.setSize(new Dimension(700,350));
		resultsSP.setLocation(25, 150);
		resultsTA.setEditable(false);
		myCP.add(resultsSP); 

		setVisible(true);
	}//constructor
	
	private void verifyURL(String theURL){
		try {
			URL verifiedURL = new URL(theURL);
		}catch (Exception e) {
			resultsTA.append("URL cannot be verified\n");
		}//catch error
		resultsTA.append("URL is verified\n");
	}//verifyURL method
	
	private void openConnection(String theURL){
		try {
			URL url = new URL(theURL);
			InputStream stream = url.openStream();
			resultsTA.append("Connection is successful\n");
			stream.close();
		}//try
		catch (Exception e){
			resultsTA.append("Connection is unsuccessful\n");
		}//catch error
	}//openConnection method
	
	private void downloadPage(String theURL){
		try {
			URL url = new URL(theURL);
			InputStream stream = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			StringBuffer pageBuffer = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null){
				pageBuffer.append(line + "\n");
			}//while
			//resultsTA.append("\nHere is the downloaded content:\n\n");
			pageContents = pageBuffer.toString();
			//resultsTA.append(pageContents+ "\n\n");
			stream.close();
		}//try block
		catch (Exception e){
			resultsTA.append("\nUnable to download page\n");
		}//catch block
	}//downloadPage method
	
	private void printHost(String theURL){
		try {
			URL url = new URL(theURL);
			String host = url.getHost();
			resultsTA.append(host + "\n");
		}catch (Exception e){
			resultsTA.append("\nError finding host\n");
		}//catch error
	}//printHost method
	
	private void printFileName(String theURL){
		try {
			URL url = new URL(theURL);
			String file = url.getFile();
			resultsTA.append(file + "\n");
		}catch (Exception e){
			resultsTA.append("\nError finding file name\n");
		}//catch error
	}//printFileName method
	
	private void retrieveLinks(){
		Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]",
			Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(pageContents);
		int counter = 0; //for numbering
		while(m.find()){
			String link = m.group(1).trim();
			if (link.indexOf("http://f-droid.org/repository/browse/?fdid=") != -1){
				counter++; 
				numberOfApps++;
				resultsTA.append("\n" + counter + ") " + link);
				downloadPage(link);
				retrieveApps();
			}//add only if it matches
		}//while 
	}//getDetails method
	
	private void retrieveApps(){
		Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]",
			Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(pageContents);
		boolean found = false;
		while(m.find() && found == false){
			String link = m.group(1).trim();
			if (link.lastIndexOf(".apk") != -1){
				appList.add(link);
				resultsTA.append("\n\t" + link);
				found = true;
			}//add only if it matches
		}//while 
	}//downloadApps method
	
	private void getPages(String theURL){
		String url; 
		for (int i = 1; i < 10; i++){
			url = theURL + i;
			downloadPage(url);
			resultsTA.append("\n\nPage " + i + " contains these Apps:\n");
			retrieveLinks();
		}//go through each page
	}//getPages method
	
	private void showAppList(){
		resultsTA.append("\n\nHere are the links we have so far: \n\n");
		for (int i = 0; i < appList.size(); i++){
			resultsTA.append(appList.get(i) + "\n");
		}//traverse appList
	}//getApps
	
	private void toggleEnabled(boolean value){
		crawling = value;
		if (crawling){
			crawlingL.setText("Crawling: ON");
		}else{
			crawlingL.setText("Crawling: OFF");
		}//else
		searchB.setEnabled(!value);
		stopB.setEnabled(value);
	}//disableButtons method
	
	public class SearchBHandler implements ActionListener {
		public void actionPerformed(ActionEvent e){
			toggleEnabled(true);
			verifyURL("http://f-droid.org/repository/browse/");
			printHost("http://f-droid.org/repository/browse/");
			printFileName("http://f-droid.org/repository/browse/");
			openConnection("http://f-droid.org/repository/browse/");
			getPages("http://f-droid.org/repository/browse/?fdpage=");
			resultsTA.append("\n\nThere is a total of "+ numberOfApps + " apps.");
			showAppList();
		}//actionPerformed method
	}//SearchBHandler
	
	public class StopBHandler implements ActionListener {
		public void actionPerformed(ActionEvent e){
			toggleEnabled(false);
			return;
		}//actionPerformed method
	}//SearchBHandler
	
	public static void main(String[] args) {
		AndroidCrawler myAppF = new AndroidCrawler();
	}//main method
}//Andriod Crawler class
