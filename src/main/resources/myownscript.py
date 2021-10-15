with open("test.log", "a") as myfile:
    myfile.write("Received: " + payload + "\n") # headers