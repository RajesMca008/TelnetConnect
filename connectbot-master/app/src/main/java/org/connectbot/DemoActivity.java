/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2017 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot;

import java.util.ArrayList;

import org.connectbot.transport.VechileData;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

/**
 * Created by rrallabandi on 3/7/2017.
 */

public class DemoActivity extends AppCompatListActivity{

	ArrayList<VechileData> vechileDataArrayList=new ArrayList<>();

	int seekBarValue=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout);


		final String lines[]={"2","3","4","5","6","7","7","6","6","5","4","3","0","�"};

		final Switch aSwitch=(Switch)findViewById(R.id.checkbox_switch);
		SeekBar seekBar=(SeekBar)findViewById(R.id.seekBar) ;


		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				 Log.i("TEST","onProgressChanged" +i);
				seekBarValue=i;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				Log.i("TEST","DATA Size " +vechileDataArrayList.size());

				for (int i=0;i<vechileDataArrayList.size();i++)
				{
					Log.i("TEST","DATA inserted" +vechileDataArrayList.get(i).getWeight());
				}
			}
		});




		new Thread()
		{
			@Override
			public void run() {
				super.run();
				int minValue=5;
				int tempWait=0;
				while (aSwitch.isChecked())
				{



							int updateWait=seekBarValue;

					//Log.i("TEST","onProgressChanged" +updateWait);

							if(minValue<updateWait )
							{
								if(updateWait>tempWait)
								tempWait=updateWait;

							}else {

								if(tempWait>minValue) {
									VechileData date = new VechileData();
									date.setWeight(tempWait);
									vechileDataArrayList.add(date);
									Log.i("TEST", "For loop break.........." + tempWait);
									tempWait = 0;
									//break;
								}
							}

				}
			}
		}.start();


		/*new Thread()
		{
			@Override
			public void run() {
				super.run();

				while (aSwitch.isChecked())
				{
					int tempWait=0;
					Log.i("TEST","Enter loop");

					for (int i=0;i<lines.length;i++)
					{

						boolean  isNumeric=lines[i].split(",")[0].matches("-?\\d+(\\.\\d+)?");



						if(isNumeric)
						{
							int updateWait=Integer.parseInt(lines[i].split(",")[0]);
							if(tempWait<updateWait)
							{
								tempWait=updateWait;
							}else {
								VechileData date=new VechileData();
								date.setWeight(tempWait);
								vechileDataArrayList.add(date);
								Log.i("TEST","For loop break..........");
								break;
							}
						}
					}
				}
			}
		}.start();*/

	}
}
