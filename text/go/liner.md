## liner

https://github.com/peterh/liner

> Liner is a command line editor with history. 

liner, an interactor cli editor。

这是一个自定义命令行编辑器。特性如下

+ 支持在平台或terminals下的一些键，例如Ctrl-C/Ctrl-L等，左右键(调光标)，上下键(调历史命令)
+ 补全功能，按Tab进行补全

写了一个简单的示例。

```

var (
	state        *liner.State
	defaultModel liner.ModeApplier
	rawModel     liner.ModeApplier
	history_f    = filepath.Join(os.TempDir(), ".liner_example_history")
	command      = []string{"hello", "hi", "who are you", "what can u do"}
	support      = true
)

/**
  将历史记录的命令读入内存
*/
func beforeNew() {
	if state != nil {
		if content, err := ioutil.ReadFile(history_f); err == nil {
			state.ReadHistory(strings.NewReader(strings.Join(strings.Split(string(content), "\n"), "\n")))
		}
	}
}

func main() {
	beforeNew()
	newLiner()

	for {
		//> 提示,模拟终端 = = 
		input, err := prompt("> ")
		if err != nil {
			//用户按了ctrl-c.  处理的是跟终端一样，废除当前输入  直接进入下一次输入
			if err == liner.ErrPromptAborted {
				continue
			}
		}
		//下面是一些low的交互.. 
		if input == "hello" {
			fmt.Println("hi")
		} else if input == "who are you" {
			fmt.Println("a robot")
		} else if input == "what can u do" {
			fmt.Println("emmmmmmm...  nothing")
		} else if input == "exit" {
			break
		} else {
          continue
		}
		//将命令加入历史，方便按上下键调出
		state.AppendHistory(input)
	}
	close()

}

func newLiner() {
	//当前模式 -- 默认模式
	defaultModel, _ = liner.TerminalMode()
	//newLiner,会返回一个State实例，并且会修改terminal的模式为rawModel
	state = liner.NewLiner()
	r, err := liner.TerminalMode()
	log.Printf("default Model is %v \n", defaultModel)
	log.Printf("after new liner, Model change to raw model: %v \n", rawModel)

	if err != nil {
		log.Printf("can not terminal Mode is %v:", err)
		support = false
	} else {
		rawModel = r
		defaultModel.ApplyMode()
	}
	state.SetCtrlCAborts(true)
	//设置补全
	state.SetCompleter(complete)
}

func prompt(p string) (string, error) {
	if support {
		rawModel.ApplyMode()
		defer defaultModel.ApplyMode()
	} else {
		fmt.Print(p)
		p = ""
		defer fmt.Println()
	}
	return state.Prompt(p)
}

func close() {
	defaultModel.ApplyMode()
	state.Close()
}

func complete(line string) (s []string) {
	if line == "" {
		return command
	} else {
		for _, c := range command {
			if strings.HasPrefix(c, strings.ToLower(line)) {
				s = append(s, c)
			}
		}
	}
	return
}

```



然后，看一下其中使用的方法

+ NewLiner

  ```
  //NewLiner initializes a new *State, and sets the terminal into raw mode. To restore the terminal to its previous state, call State.Close().
  func NewLiner() *State
  ```

  初始化一个*state，设置terminal模式为raw mode

+ Prompt

  ```
  //Prompt displays p and returns a line of user input, not including a trailing newline character. An io.EOF error is returned if the user signals end-of-file by pressing Ctrl-D. Prompt allows line editing if the terminal supports it.
  func (s *State) Prompt(prompt string) (string, error)
  ```

  Prompt方法显示prompt参数，返回用户输入的行，不包括换行符。这个方法是获取用户输入的方法，调用Prompt方法，一直阻塞，知道用户输入有效行，然后返回。

+ TerminalMode

  ```
  //TerminalMode returns the current terminal input mode as an InputModeSetter.
  func TerminalMode() (ModeApplier, error)
  ```

  返回当前terminal的模式，另外提一句，ModeApplier实例调用ApplyMode方法即可应用模式。

  那么，这个模式到底是啥呢。这个问题我思索了很久。未果。

  搜寻了一番，结论大概是，这个是真的是terminal，mac下的terminal的参数有很多(例如，IFlag, OFlag等，这些参数决定了terminal的输入输出来源等等)，然后这些参数的不同值的组合(或者继承这些参数，新增别的参数等)大概就形成了不同模式= =

  基本呢，是可以忽略的，liner中会修改模式的地方，在于newLiner初始化\*State时会修改模式为raw，然后\*State的close方法会复原模式为初始模式。所以，在使用完state之后记得close掉就可以拉。

  要是不close呢，真的会有影响吗？是的，真的会有。程序结束返回到命令行的时候，就会发现输入有问题…emmm，还是老老实实记得close。关于terminal的模式，搜了一番，实力太菜没看到合适的文档介绍

+ Close

  ```
  //Close returns the terminal to its previous mode
  func (s *State) Close() error
  ```

  关闭设置terminal为之前的模式





然后还有别的一些方法，包括PasswordPrompt(输入密码使用)，SetMultiLineMode（设置是否允许多行模式）等等..见[doc](https://godoc.org/github.com/peterh/liner)

