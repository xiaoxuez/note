
#!/bin/sh
list_alldir(){
    for file2 in `ls $1`
    do
        if [ x"$file2" != x"." -a x"$file2" != x".." ];then
            if [ -d "$1/$file2" ];then
                echo -e "\033[32;49;1m${file2}\033[39;49;0m"
                list_alldir "$1/$file2"
            elif [ -r "$1/$file2" ];then
              echo -n "$file2                               $1/$file2"
            fi
        fi
        echo ""
    done
}
echo "$1"
list_alldir "$1"
