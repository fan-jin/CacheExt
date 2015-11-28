import sys

#python genexp.py s:800:600 a:200:300 a:700:600 a:100:300 a:800:600 s:1100:600
#python genexp.py s:200:100 s:300:300 s:800:400 a:200:200 a:300:300 a:800:400 s:1100:600
#python genexp.py s:200:100 s:300:300 s:800:400 a:100:200 a:300:1000 a:600:500 a:400:200 a:600:1000 a:800:1000 s:1100:1200
#python genexp.py s:1100:600

def main():
    playerStart = 0
    # Get playerEnd

    f = open('expgen.dyn', 'w')

    playerPointer = 0

    # For each argument...
    for arg in sys.argv[1:]:
        
        op = arg.split(":")[0]
        target = int(arg.split(":")[1])
        wait = int(arg.split(":")[2])
        if len(arg.split(":")) > 3:
            extra = int(arg.split(":")[3])

        #playerEnd = int(sys.argv[1])
        
        if op=="s":
            
            f.write("% INCREASE...\n\n")
            
            # Accept players
            for i in range(playerPointer, target):
                f.write("+1\n")
                f.write("sleep " + str(wait) + "\n")
                playerPointer += 1
                f.write("%" + str(playerPointer) + " players\n")
                
        elif op=="a":

            # Activate or deactivate
            if target>=playerPointer:
                activate_string = "true"
                rangeStart=playerPointer+1
                rangeEnd=target+1
                rangeStep=1
                f.write("\n% ACTIVATE...\n\n")
            else:
                activate_string = "false"
                rangeStart=playerPointer
                rangeEnd=target
                rangeStep=-1
                f.write("\n% DEACTIVATE...\n\n")

            for i in range(rangeStart-1, rangeEnd-1, rangeStep):
                f.write("activate " + str(i) + " " + str(i+1) + " " + activate_string + " false " + "\n")
                f.write("sleep " + str(wait) + "\n")
                playerPointer += rangeStep
                f.write("%" + str(playerPointer) + " players\n")

        elif op=="f" or op=="u":

            # Flock or unflock
            if op=="f":
                flock="true"
            elif op=="u":
                flock="false"

            # For this special operation reassign fields...
            start = target
            end = wait
            waitTime = extra

            for i in range(start, end, 1):
                f.write("activate " + str(i) + " " + str(i+1) + " " + "true" + " " + flock + "\n")
                f.write("sleep " + str(waitTime) + "\n")
                    
                    
    f.close()

if __name__ == "__main__":
    main()
