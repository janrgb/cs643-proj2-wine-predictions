# Wine Quality Predictions

By Janish Suneja

## Setting Up The Environment

### Setting up 6 Ubuntu EC2 Instances

Log on to AWS and do the following:

- Go to **EC2** and select **Launch Instances**.

- Name your instance. This will be changed later.

- In the **Quick Start** pane, select **Ubuntu**.

- Under **Instance Type**, it may be beneficial to select a `t3.small` instance.

- Generate a key pair and save it to your local machine with limited privileges. You will need it later.

```
chmod 400 your-key.pem
```

- Under **Network Settings**, change the SSH rule from **Anywhere** to **My IP**. Leave everything else the same.

- In the pane on the right, set **Number of Instances** to 6. 

- Hit **Launch**.

### Configuring Networking Across All Instances

These instances were created under the same security group. We need to give some light permissions so they can all communicate with one
another.

Select one of the instances and navigate to the **Security** tab.

- Find a link to the security group in the SSH inbound rule. It should say something like `launch-wizard-#`. Click it.

- Check the security group and go to the **Inbound Rules** tab. Click **Edit Inbound Rules**.

- Click **Add Rule**.

    - Set **Type** to **All TCP**

    - Set **Protocol** to **TCP**

    - Set **Port Range** to `0 - 65535`

    - Set **Source** to `Custom`

    - For the value of the Source, put in the ID for the security group. It should start with **sg** and then a number of characters. An example for `launch-wizard-4` would be `sg-01c83ec17fbb74aed`. Yours will be different.

- Hit **Save Rules**.

### Renaming the Instances

You will have one **master** instance and four **worker** instances for training. The last instance will be for running the prediction app both locally and with Docker.

- Hover over an instance.

- Select the pencil icon. From here you can rename it.

- Rename one to "master", four to "worker#" where # is the number, and one to "runner".

### Preparing the EC2 Instances

- Click on the **master** instance and hit the **Connect** button.

- Go to the **SSH Client** tab. Follow the example to SSH in.

```bash
ssh -i "your-key.pem" ubuntu@public-dns
```

- Repeat this process for all the worker machines and the runner machine.

- Do the following on every EC2 instance once you get a shell to get the proper software:

```bash
sudo apt update

# Install Java
sudo apt install -y default-jdk && java -version

sudo apt update

# Install Maven build system
sudo apt install -y maven && mvn -v

# Install Apache Spark
curl -O https://dlcdn.apache.org/spark/spark-4.1.1/spark-4.1.1-bin-hadoop3.tgz
tar -xzvf spark-4.1.1-bin-hadoop3.tgz
rm spark-4.1.1-bin-hadoop3.tgz

# Check if spark works
~/spark-4.1.1-bin-hadoop3/bin/spark-shell
```

- From your local machine, copy the .pem key to the master server. You'll need it there, too.

```
scp -i "your-key.pem" your-key.pem ubuntu@master-dns:/home/ubuntu
```

## Running the Training

### Starting the Master

Go to the terminal on the **master** node and clone this repo on the home directory:

```bash
git clone https://github.com/janrgb/cs643-proj2-wine-predictions.git
```

Go inside the repo and copy `TrainingDataset.csv` to the ubuntu user's home directory:

```bash
cp TrainingDataset.csv ~
```

Now we are ready to start the master.

- Run `~/spark-4.1.1-bin-hadoop3/sbin/start-master.sh`

- Run `curl localhost:8080` and examine the HTML to find the URL for your master. Take note of it.

    - It will look something like: `spark://ip-<YOUR-MASTER-MACHINE-IP>.ec2.internal:7077`

### Starting the Workers

The master should now be running. Now we have to start the workers.

- From the master machine, copy `TrainingDataset.csv` to every worker machine. You can do so with `scp`.

```bash
scp -i "your-key.pem" TrainingDataset.csv ubuntu@public-dns1:/home/ubuntu

scp -i "your-key.pem" TrainingDataset.csv ubuntu@public-dns2:/home/ubuntu

scp -i "your-key.pem" TrainingDataset.csv ubuntu@public-dns3:/home/ubuntu

scp -i "your-key.pem" TrainingDataset.csv ubuntu@public-dns4:/home/ubuntu
```

- For every worker instance, run `spark-4.1.1-bin-hadoop3/sbin/start-worker.sh spark://ip-<YOUR-MASTER-MACHINE-IP>.ec2.internal:7077`

### Running the Training App

Now we can run the training app. Navigate back to your master machine.

- `cd` into the git directory.

- Run `mvn clean package` to get everything needed.

- After that, look for a script called `run_training.sh`.

```bash
cat run_training.sh

/home/ubuntu/spark-4.1.1-bin-hadoop3/bin/spark-submit \
  --class WineQualityTraining \
  --master spark://YOUR_SPARK_URL:7077 \
  --executor-memory 512M \
  --total-executor-cores 8 \
target/simple-project-1.0.jar
```

- You should edit the `--master` option to be equal to your master's url.

- The specs are fine for t3.small instances, but you may want to mess around with these if your instances are smaller than that.

- When satisfied, run the script:

```bash
./run_training.sh
```

- If you see a bunch of `INFO` or `WARN` tags, don't worry about it.

- You should see `pipelineModel/` outputted to the home directory of both the master and all the workers.

### Consolidating /pipelineModel

These directories need to be consolidated before they can be used as a model.

- Run the following on the master. Replace `WORKER_IPS` with your actual worker IPs:

```bash
for ip in WORKER_IP_1 WORKER_IP_2 WORKER_IP_3 WORKER_IP_4; do
  rsync -avz -e "ssh -i ~/my-key.pem" ubuntu@$ip:/home/ubuntu/pipelineModel/ /home/ubuntu/pipelineModel/
done
```

- Then run the following on the master to clean up:

```bash
mv pipelineModel/metadata/_temporary/0/task_*/part* pipelineModel/metadata/

mv pipelineModel/stages/0_strIdx_*/data/_temporary/0/task_*/part* pipelineModel/stages/0_strIdx_*/data/
mv pipelineModel/stages/0_strIdx_*/metadata/_temporary/0/task_*/part* pipelineModel/stages/0_strIdx_*/metadata/

mv pipelineModel/stages/1_vecAssembler_*/metadata/_temporary/0/task_*/part* pipelineModel/stages/1_vecAssembler_*/metadata/

mv pipelineModel/stages/2_stdScal_*/data/_temporary/0/task_*/part* pipelineModel/stages/2_stdScal_*/data/
mv pipelineModel/stages/2_stdScal_*/metadata/_temporary/0/task_*/part* pipelineModel/stages/2_stdScal_*/metadata/

mv pipelineModel/stages/3_logreg_*/data/_temporary/0/task_*/part* pipelineModel/stages/3_logreg_*/data/
mv pipelineModel/stages/3_logreg_*/metadata/_temporary/0/task_*/part* pipelineModel/stages/3_logreg_*/metadata/

find pipelineModel/ -name "_temporary" -type d -exec rm -rf {} +
```

- You should now have a proper `pipelineModel` that looks about the same as the one in the git repo.

## Running the Testing

### Setting Up the Runner

This section explains how to set up the runner before moving on.

- Navigate to the **runner** EC2 instance.

- Clone this repo to the runner's home directory:

```bash
git clone https://github.com/janrgb/cs643-proj2-wine-predictions.git
```

### Local Running

Local running is accomplished with the `run_predictor.sh` bash script in the git repo.

- If you try running it without an argument, it will fail. The program takes in a CSV as input.

- Provide a legitimate path and run the program.

```bash
./run_predictor.sh ValidationDataset.csv
```

- If you see a bunch of INFO or WARN tags, don't worry about it. Look for the F1-Score: it will be buried in between.

### Docker Running

Docker running is accomplished by pulling the image from **Docker Hub** and then running it.

- First, install Docker on the **runner**. Follow the guide at [https://docs.docker.com/engine/install/ubuntu/]()

- Then, run the following:

```

```
