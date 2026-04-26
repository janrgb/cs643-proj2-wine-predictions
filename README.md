# Wine Quality Predictions

By Janish Suneja

## Setting Up The Environment

### Setting up 6 Ubuntu EC2 Instances

Log on to AWS and do the following:

- Go to **EC2** and select **Launch Instances**.

- Name your instance. This will be changed later.

- In the **Quick Start** pane, select **Ubuntu**.

- Under **Instance Type**, it may be beneficial to select a `t3.small` instance.

- Generate a key pair and save it to your local machine. You will need it later.

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

## Running the Training

### Starting the Master

Go to the terminal on the **master** node and clone this repo on the home directory:

```bash
git clone https://github.com/janrgb/cs643-proj2-wine-predictions.git
```

Go inside the repo and copy `TrainingDataset.csv` to the ubuntu user's home directory:

```bash
mv TrainingDataset.csv ~
```

Now we are ready to start the master.

- Run `~/spark-4.1.1-bin-hadoop3/sbin/start-master.sh`
