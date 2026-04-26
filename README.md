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

    - For the value of the Source, put in the ID for the security group. It should start with **sg** and then a number of characters. An example for `launch-wizard-4` would be `sg-01c83ec17fbb74aed*`
