    package at.danielhuber.training;

    import android.content.res.AssetManager;
    import android.database.Cursor;
    import android.os.Bundle;
    import android.support.v7.app.ActionBarActivity;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.Button;
    import android.widget.NumberPicker;
    import android.widget.Toast;

    import weka.classifiers.functions.MultilayerPerceptron;
    import weka.core.Attribute;
    import weka.core.DenseInstance;
    import weka.core.FastVector;
    import weka.core.Instance;
    import weka.core.Instances;

    import java.io.InputStream;
    import java.io.ObjectInputStream;

    public class MainActivity extends ActionBarActivity {

        DatabaseHelper myDB;
        NumberPicker np1;
        NumberPicker np2;
        NumberPicker np3;
        Button predictButton;
        Button saveButton;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            myDB =new DatabaseHelper(this);

            np1 = (NumberPicker) findViewById(R.id.numberPicker1);
            np2 = (NumberPicker) findViewById(R.id.numberPicker2);
            np3 = (NumberPicker) findViewById(R.id.numberPicker3);

            np1.setMaxValue(500);
            np1.setMinValue(0);
            np1.setValue(10);

            np2.setMaxValue(500);
            np2.setMinValue(0);
            np2.setValue(30);

            np3.setMaxValue(1000);
            np3.setMinValue(0);
            np3.setValue(50);

            predictButton =(Button) findViewById(R.id.predictbutton);

            saveButton = (Button) findViewById(R.id.savebutton);
        }

        public void addData(){
            saveButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                         boolean isInserted= myDB.insertData(np1.getValue(),np2.getValue(),np3.getValue());
                            if (isInserted==true){
                                Toast.makeText(MainActivity.this,"Data inserted",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(MainActivity.this,"Insertion Failed",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);

            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        public void predict(){
            predictButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //get cursor position
                           Cursor res= myDB.getData();
                            res.moveToLast();

                            try {
                                //deserialize model
                               MultilayerPerceptron mlp = null; //object to be deserialized
                                InputStream is = null;
                                ObjectInputStream ois=null;
                                AssetManager assets = getAssets();
                                is = assets.open("exermodel.model");
                                ois = new ObjectInputStream(is);
                                mlp = (MultilayerPerceptron) ois.readObject();

                                //create instances on the fly
                                Attribute Attribute1 = new Attribute("3back");
                                Attribute Attribute2 = new Attribute("2back");
                                Attribute Attribute3 = new Attribute("1back");

                            FastVector fvExercise = new FastVector(3);
                            fvExercise.addElement("push-up");
                            fvExercise.addElement("pull-up");
                            fvExercise.addElement("squat");
                            Attribute Attribute4 = new Attribute("exercise",fvExercise);


                            FastVector fvClass = new FastVector(4);
                            fvClass.addElement("slow");
                            fvClass.addElement("medium");
                            fvClass.addElement("fast");
                            fvClass.addElement("overtraining");
                            Attribute ClassAttribute = new Attribute("class",fvClass);

                            FastVector fvAll = new FastVector(5);
                            fvAll.addElement(Attribute1);
                            fvAll.addElement(Attribute2);
                            fvAll.addElement(Attribute3);
                            fvAll.addElement(Attribute4);
                            fvAll.addElement(ClassAttribute);

                            Instances unlabeled = new Instances("Rel",fvAll,3);

                            // set class attribute
                            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

                            for(int i=1;i<4;i++) {
                                res.moveToLast();
                                Instance inst = new DenseInstance(5);

                                inst.setValue((Attribute) fvAll.elementAt(4), "slow"); //dummy value

                                inst.setValue((Attribute) fvAll.elementAt(3), getExercise(i));
                                inst.setValue((Attribute) fvAll.elementAt(2), res.getInt(i));
                                res.moveToPrevious();
                                inst.setValue((Attribute) fvAll.elementAt(1), res.getInt(i));
                                res.moveToPrevious();
                                inst.setValue((Attribute) fvAll.elementAt(0), res.getInt(i));

                                unlabeled.add(inst);
                            }



                            // create copy
                            Instances labeled = new Instances(unlabeled);


                            // label instances
                            for (int i = 0; i < unlabeled.numInstances(); i++) {
                                double mlpLabel =  mlp.classifyInstance(unlabeled.instance(i));
                                labeled.instance(i).setClassValue(mlpLabel);
                            }


                            np1.setValue(setNumPicker(labeled.instance(0).stringValue(ClassAttribute),1));
                                np2.setValue(setNumPicker(labeled.instance(1).stringValue(ClassAttribute),2));
                                np3.setValue(setNumPicker(labeled.instance(2).stringValue(ClassAttribute),3));


                                Toast.makeText(MainActivity.this,labeled.instance(1).stringValue(ClassAttribute),
                                        Toast.LENGTH_SHORT).show();

                            }
                            catch(Exception e){
                                Toast.makeText(MainActivity.this,"Less than 3 entries in Database",
                                        Toast.LENGTH_SHORT).show();
                            }
                            finally {

                            }
                        }

                    }
            );
        }
        private int setNumPicker(String label,int i){

            Cursor res = myDB.getData();
            res.moveToLast();
            int avg = res.getInt(i);
            res.moveToPrevious();
            avg+= res.getInt(i);
            res.moveToPrevious();
            avg+= res.getInt(i);
            avg=avg/3;

            switch(label){
                case "slow": return checkPositive(avg+2);
                case "medium": return checkPositive(avg+2);
                case "fast": return checkPositive(avg+3);
                case "overtraining": return checkPositive(avg-3);
                default: return 200;
            }

        }
        private int checkPositive(int value){
            if(value>0){
                return value;
            }
            else return 0;
        }

        private String getExercise(int i){
            switch(i){
                case 1: return "pull-up";
                case 2: return "push-up";
                default: return "squat";

            }
        }
    }
